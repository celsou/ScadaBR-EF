package com.serotonin.mango.rt.maint.work;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.mango.rt.dataImage.PointLinkSetPointSource;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;

public class PointLinkSetPointWorkItem extends SetPointWorkItem {
	private static final Log LOG = LogFactory.getLog(PointLinkSetPointWorkItem.class);

	private PointLinkSetPointSource plSource;

	/**
	 * @param targetPointId
	 * @param pvt
	 * @param source
	 */
	public PointLinkSetPointWorkItem(int targetPointId, PointValueTime pvt, PointLinkSetPointSource source) {
		super(targetPointId, pvt, (SetPointSource) source);
		this.plSource = source;
	}

	@Override
	public void execute() {
		super.execute();
		plSource.pointSetComplete();
	}

}
