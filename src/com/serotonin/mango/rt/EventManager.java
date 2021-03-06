/*
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.rt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.EventDao;
import com.serotonin.mango.rt.event.AlarmLevels;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.rt.event.handlers.EmailHandlerRT;
import com.serotonin.mango.rt.event.handlers.EventHandlerRT;
import com.serotonin.mango.rt.event.type.DataPointEventType;
import com.serotonin.mango.rt.event.type.DataSourceEventType;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.rt.event.type.SystemEventType;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.event.EventHandlerVO;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.util.ILifecycle;
import com.serotonin.web.i18n.LocalizableMessage;

import br.org.scadabr.vo.userCache.UserCache;

/**
 * @author Matthew Lohbihler
 */
public class EventManager implements ILifecycle {
	private final Log log = LogFactory.getLog(EventManager.class);

	private final List<EventInstance> activeEvents = new CopyOnWriteArrayList<EventInstance>();

	class MySet<T> extends HashSet<T> {
		/**
		 * Class MySet to override to turn both add and remove synchronized.
		 */
		private static final long serialVersionUID = 304602545205487894L;

		public synchronized void addRemove(int pr, T value) {
			if (pr == 0) {
				super.add(value);
			} else {
				super.remove(value);
			}
		}
	}

	private final MySet<Integer> activeDatapointIds = new MySet<Integer>();
	private final Map<Integer, AtomicInteger> activeUserEventCount = new HashMap<Integer, AtomicInteger>();

	class MyMap<K, V> extends HashMap<K, V> {
		/**
		 * Class MyMap to override both put and remove at the same time.
		 */
		private static final long serialVersionUID = -3170412289216738463L;

		public synchronized void putRemove(int pr, K key, V value) {
			if (pr == 0) {
				super.put(key, value);
			} else {
				super.remove(key);
			}
		}
	}

	private final MyMap<Integer, List<Integer>> eventUserPair = new MyMap<Integer, List<Integer>>();
	private EventDao eventDao;
	private UserCache userCache;
	private long lastAlarmTimestamp = 0;
	private int highestActiveAlarmLevel = 0;

	//
	//
	// Basic event management.
	//
	public void raiseEvent(EventType type, long time, boolean rtnApplicable, int alarmLevel, LocalizableMessage message,
			Map<String, Object> context) {
		// Check if there is an event for this type already active.
		EventInstance dup = get(type);
		if (dup != null) {
			// Check the duplicate handling.
			int dh = type.getDuplicateHandling();
			if (dh == EventType.DuplicateHandling.DO_NOT_ALLOW) {
				// Create a log error...
				log.error("An event was raised for a type that is already active: type=" + type + ", message="
						+ message.getKey());
				// ... but ultimately just ignore the thing.
				return;
			}

			if (dh == EventType.DuplicateHandling.IGNORE)
				// Safely return.
				return;

			if (dh == EventType.DuplicateHandling.IGNORE_SAME_MESSAGE) {
				// Ignore only if the message is the same. There may be events
				// of this type with different messages,
				// so look through them all for a match.
				for (EventInstance e : getAll(type)) {
					if (e.getMessage().equals(message))
						return;
				}
			}

			// Otherwise we just continue...
		}

		// Determine if the event should be suppressed.
		boolean suppressed = isSuppressed(type);

		EventInstance evt = new EventInstance(type, time, rtnApplicable, alarmLevel, message, context);

		if (!suppressed)
			setHandlers(evt);

		// Get id from database by inserting event immediately.
		eventDao.saveEvent(evt);

		// Create user alarm records for all applicable users
		List<Integer> eventUserIds = new ArrayList<Integer>();
		Set<String> emailUsers = new HashSet<String>();

		for (User user : userCache.getActiveUsers()) {
			// Do not create an event for this user if the event type says the
			// user should be skipped.
			if (type.excludeUser(user))
				continue;

			log.debug("User: " + user.getUsername() + ", id: " + user.getId());

			// Workaround - maybe we have multiple instances of the same user in
			// the cache
			if (Permissions.hasEventTypePermission(user, type) && !eventUserIds.contains(user.getId())) {
				log.debug("Adding user: " + user.getUsername() + ", id: " + user.getId());
				eventUserIds.add(user.getId());
				if (evt.isAlarm() && user.getReceiveAlarmEmails() > 0 && alarmLevel >= user.getReceiveAlarmEmails())
					emailUsers.add(user.getEmail());
			}
		}

		log.debug("eventUserIds.size: " + eventUserIds.size());
		if (eventUserIds.size() > 0) {
			eventDao.insertUserEvents(evt.getId(), eventUserIds, evt.isAlarm());
			if (!suppressed && evt.isAlarm())
				lastAlarmTimestamp = System.currentTimeMillis();
		}

		if (evt.isRtnApplicable()) {
			activeEvents.add(evt);
			activeDatapointIds.addRemove(0, evt.getEventType().getDataPointId());
			if (evt.getEventType().getEventSourceId() == EventType.EventSources.DATA_POINT
					&& evt.getAlarmLevel() != AlarmLevels.NONE) {
				eventUserPair.putRemove(0, evt.getId(), eventUserIds);
				for (Integer id : eventUserIds) {
					// userEventPair.put(key, value)
					if (activeUserEventCount.get(id) == null) {
						activeUserEventCount.put(id, new AtomicInteger(1));
					} else {
						activeUserEventCount.get(id).incrementAndGet();
					}
				}
			}
		}

		if (suppressed)
			eventDao.ackEvent(evt.getId(), time, 0, EventInstance.AlternateAcknowledgementSources.MAINTENANCE_MODE);
		else {
			if (evt.isRtnApplicable()) {
				if (alarmLevel > highestActiveAlarmLevel) {
					int oldValue = highestActiveAlarmLevel;
					highestActiveAlarmLevel = alarmLevel;
					SystemEventType.raiseEvent(new SystemEventType(SystemEventType.TYPE_MAX_ALARM_LEVEL_CHANGED), time,
							false, getAlarmLevelChangeMessage("event.alarmMaxIncreased", oldValue));
				}
			}

			// Call raiseEvent handlers.
			handleRaiseEvent(evt, emailUsers);

			if (log.isDebugEnabled())
				log.debug(
						"Event raised: type=" + type + ", message=" + message.getLocalizedMessage(Common.getBundle()));
		}
	}

	public void returnToNormal(EventType type, long time) {
		returnToNormal(type, time, EventInstance.RtnCauses.RETURN_TO_NORMAL);
	}

	public void returnToNormal(EventType type, long time, int cause) {
		EventInstance evt = remove(type);

		// Loop in case of multiples
		while (evt != null) {
			resetHighestAlarmLevel(time, false);

			evt.returnToNormal(time, cause);
			eventDao.saveEvent(evt);

			// Call inactiveEvent handlers.
			handleInactiveEvent(evt);

			// Check for another
			evt = remove(type);
		}

		if (log.isDebugEnabled())
			log.debug("Event returned to normal: type=" + type);
	}

	private void deactivateEvent(EventInstance evt, long time, int inactiveCause) {
		activeEvents.remove(evt);
		removeFromControlLists(evt);

		resetHighestAlarmLevel(time, false);
		evt.returnToNormal(time, inactiveCause);
		eventDao.saveEvent(evt);

		// Call inactiveEvent handlers.
		handleInactiveEvent(evt);
	}

	public long getLastAlarmTimestamp() {
		return lastAlarmTimestamp;
	}

	//
	//
	// Canceling events.
	//
	public void cancelEventsForDataPoint(int dataPointId) {
		for (EventInstance e : activeEvents) {
			if (e.getEventType().getDataPointId() == dataPointId)
				deactivateEvent(e, System.currentTimeMillis(), EventInstance.RtnCauses.SOURCE_DISABLED);
		}
	}

	public void cancelEventsForDataSource(int dataSourceId) {
		for (EventInstance e : activeEvents) {
			if (e.getEventType().getDataSourceId() == dataSourceId)
				deactivateEvent(e, System.currentTimeMillis(), EventInstance.RtnCauses.SOURCE_DISABLED);
		}
	}

	public void cancelEventsForPublisher(int publisherId) {
		for (EventInstance e : activeEvents) {
			if (e.getEventType().getPublisherId() == publisherId)
				deactivateEvent(e, System.currentTimeMillis(), EventInstance.RtnCauses.SOURCE_DISABLED);
		}
	}

	private void resetHighestAlarmLevel(long time, boolean init) {
		int max = 0;
		for (EventInstance e : activeEvents) {
			if (e.getAlarmLevel() > max)
				max = e.getAlarmLevel();
		}

		if (!init) {
			if (max > highestActiveAlarmLevel) {
				int oldValue = highestActiveAlarmLevel;
				highestActiveAlarmLevel = max;
				SystemEventType.raiseEvent(new SystemEventType(SystemEventType.TYPE_MAX_ALARM_LEVEL_CHANGED), time,
						false, getAlarmLevelChangeMessage("event.alarmMaxIncreased", oldValue));
			} else if (max < highestActiveAlarmLevel) {
				int oldValue = highestActiveAlarmLevel;
				highestActiveAlarmLevel = max;
				SystemEventType.raiseEvent(new SystemEventType(SystemEventType.TYPE_MAX_ALARM_LEVEL_CHANGED), time,
						false, getAlarmLevelChangeMessage("event.alarmMaxDecreased", oldValue));
			}
		}
	}

	private LocalizableMessage getAlarmLevelChangeMessage(String key, int oldValue) {
		return new LocalizableMessage(key, AlarmLevels.getAlarmLevelMessage(oldValue),
				AlarmLevels.getAlarmLevelMessage(highestActiveAlarmLevel));
	}

	//
	//
	// Lifecycle interface
	//
	public void initialize() {
		eventDao = new EventDao();
		userCache = Common.ctx.getUserCache();

		// Get all active events and users from the database.
		List<EventInstance> evList = eventDao.getActiveEvents();
		List<User> userList = userCache.getActiveUsers();

		// Initialize CACHE

		for (EventInstance ev : evList) {
			if (ev.isRtnApplicable()) {
				activeEvents.add(ev);
				activeDatapointIds.addRemove(0, ev.getEventType().getDataPointId());
				// Create user alarm records for all applicable users
				List<Integer> eventUserIds = new ArrayList<Integer>();

				for (User user : userList) {
					// Do not create an event for this user if the event type
					// says the
					// user should be skipped.
					if (ev.getEventType().excludeUser(user))
						continue;

					if (Permissions.hasEventTypePermission(user, ev.getEventType())) {
						eventUserIds.add(user.getId());
					}
				}
				if (ev.getEventType().getEventSourceId() == EventType.EventSources.DATA_POINT
						&& ev.getAlarmLevel() != AlarmLevels.NONE) {
					eventUserPair.putRemove(0, ev.getId(), eventUserIds);
					for (Integer id : eventUserIds) {
						// userEventPair.put(key, value)
						if (activeUserEventCount.get(id) == null) {
							activeUserEventCount.put(id, new AtomicInteger(1));
						} else {
							activeUserEventCount.get(id).incrementAndGet();
						}
					}
				}
			}
		}
		lastAlarmTimestamp = System.currentTimeMillis();
		resetHighestAlarmLevel(lastAlarmTimestamp, true);
	}

	public void terminate() {
		// no op
	}

	public void joinTermination() {
		// no op
	}

	//
	//
	// Convenience
	//
	/**
	 * Returns the first event instance with the given type, or null is there is
	 * none.
	 */
	private EventInstance get(EventType type) {
		for (EventInstance e : activeEvents) {
			if (e.getEventType().equals(type))
				return e;
		}
		return null;
	}

	private List<EventInstance> getAll(EventType type) {
		List<EventInstance> result = new ArrayList<EventInstance>();
		for (EventInstance e : activeEvents) {
			if (e.getEventType().equals(type))
				result.add(e);
		}
		return result;
	}

	/**
	 * Finds and removes the first event instance with the given type. Returns
	 * null if there is none.
	 * 
	 * @param type
	 * @return
	 */
	private EventInstance remove(EventType type) {
		for (EventInstance e : activeEvents) {
			if (e.getEventType().equals(type)) {
				activeEvents.remove(e);
				removeFromControlLists(e);
				return e;
			}
		}
		return null;
	}

	private void removeFromControlLists(EventInstance e) {
		if (eventUserPair.containsKey(e.getId())) {
			for (Integer id : eventUserPair.get(e.getId())) {
				activeUserEventCount.get(id).decrementAndGet();
			}
		}
		eventUserPair.putRemove(1, e.getId(), null);
		activeDatapointIds.addRemove(1, e.getEventType().getDataPointId());
	}

	private void setHandlers(EventInstance evt) {
		List<EventHandlerVO> vos = eventDao.getEventHandlers(evt.getEventType());
		List<EventHandlerRT> rts = null;
		for (EventHandlerVO vo : vos) {
			if (!vo.isDisabled()) {
				if (rts == null)
					rts = new ArrayList<EventHandlerRT>();
				rts.add(vo.createRuntime());
			}
		}
		if (rts != null)
			evt.setHandlers(rts);
	}

	private void handleRaiseEvent(EventInstance evt, Set<String> defaultAddresses) {
		if (evt.getHandlers() != null) {
			for (EventHandlerRT h : evt.getHandlers()) {
				h.eventRaised(evt);

				// If this is an email handler, remove any addresses to which it
				// was sent from the default addresses
				// so that the default users do not receive multiple
				// notifications.
				if (h instanceof EmailHandlerRT) {
					for (String addr : ((EmailHandlerRT) h).getActiveRecipients())
						defaultAddresses.remove(addr);
				}
			}
		}

		if (!defaultAddresses.isEmpty()) {
			// If there are still any addresses left in the list, send them the
			// notification.
			EmailHandlerRT.sendActiveEmail(evt, defaultAddresses);
		}
	}

	private void handleInactiveEvent(EventInstance evt) {
		if (evt.getHandlers() != null) {
			for (EventHandlerRT h : evt.getHandlers())
				h.eventInactive(evt);
		}
	}

	private boolean isSuppressed(EventType eventType) {
		if (eventType instanceof DataSourceEventType)
			// Data source events can be suppressed by maintenance events.
			return Common.ctx.getRuntimeManager().isActiveMaintenanceEvent(eventType.getDataSourceId());

		if (eventType instanceof DataPointEventType)
			// Data point events can be suppressed by maintenance events on
			// their data sources.
			return Common.ctx.getRuntimeManager().isActiveMaintenanceEvent(eventType.getDataSourceId());

		return false;
	}

	public Integer getActiveAlarmCountPerUser(Integer userId) {
		if (this.activeUserEventCount.get(userId) != null) {
			return this.activeUserEventCount.get(userId).intValue();
		} else {
			return new Integer(0);
		}
	}

	public boolean datapointHasActiveEvent(int dpId) {
		if (activeDatapointIds.contains(new Integer(dpId))) {
			return true;
		}
		return false;
	}

	public List<EventInstance> getActiveEventsByUser(int userId) {
		List<EventInstance> filteredList = new ArrayList<EventInstance>();
		for (EventInstance e : activeEvents) {
			if (eventUserPair.containsKey(e.getId())) {
				for (Integer id : eventUserPair.get(e.getId())) {
					if (id.intValue() == userId) {
						filteredList.add(e);
					}
				}
			}
		}
		return filteredList;
	}

	public int getHighestActiveEventLevelByUser(int userId) {
		int result = -1;
		for (EventInstance e : activeEvents) {
			if (eventUserPair.containsKey(e.getId())) {
				for (Integer id : eventUserPair.get(e.getId())) {
					if (id.intValue() == userId) {
						result = e.getAlarmLevel() > result ? e.getAlarmLevel() : result;
					}
				}
			}
		}
		return result;
	}
}
