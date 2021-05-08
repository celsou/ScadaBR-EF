# ScadaBR-EF
## Sobre
O ScadaBR-EF (Enhanced Font-end) é um projeto experimental, sem qualquer vínculo com o ScadaBR original ou com o Scada-LTS. A principal intenção desse projeto é gerar uma versão do ScadaBR estável e atualizada com as tecnologias disponíveis em 2021, através de um front-end com várias melhorias de visual e usabilidade. Além disso, o ScadaBR-EF traz diversas correções de pequenos bugs que melhoram a experiência do usuário no dia a dia.

## O ScadaBR-EF é estável?
Em teoria, sim. Uma vez que o foco desse projeto é melhorar o front-end do ScadaBR, poucas foram as alterações no código Java em si. Portanto, o ScadaBR-EF deveria ser tão estável quanto o ScadaBR 1.1 CE, no qual foi baseado. Entretanto, como eu não pude realizar testes em cenários reais (dos protocolos de comunicação), não é possível garantir que o ScadaBR-EF realmente funcione de forma satisfatória.

## ScadaBR-EF vs. ScadaBR 1.1CE vs. Scada-LTS
O ScadaBR-EF não foi criado com qualquer intenção de ser um concorrente do Scada-LTS ou uma divisão no projeto ScadaBR original. O Scada-LTS é muito superior ao ScadaBR-EF, entretanto, como seu desenvolvimento ainda não chegou a uma versão "final" estável, e como o ScadaBR atualmente está preso ao Java 6, o ScadaBR-EF foi pensado para possivelmente poder ser adotado como um "intermediário" para preencher a lacuna entre o ScadaBR atual (1.0/1.1) e o futuro Scada-LTS.

## Em que o ScadaBR-EF roda?
Eu estou executando com sucesso o ScadaBR-EF no Linux Mint 19.1 (baseado no Ubuntu 18.04) com OpenJDK 8 (equivale ao Java 8) e Tomcat 8.5.39 (provavelmente rode no Tomcat 9 também).
Meus arquivos **.war** foram compilados no Eclipse, versão 2020-12 (4.18.0).
O código-fonte eu puxei do SourceForge e trouxe para o GitHub. Sinceramente, não faço ideia se fora do meu computador esse repositório consegue ser clonado e compilar com sucesso.

## Bugs conhecidos
- Atualmente, a versão da FUScaBR incluída no ScadaBR-EF (1.0) não consegue lidar com _data points_ binários, convertendo **true** para **1** e **false** para **0** na hora de criar gráficos.
- Eu experimentei problemas com o OpenJDK 8 na hora de enviar e-mails. Caso você receba um alarme de erro contendo a mensagem `javax.net.ssl.SSLHandshakeException: No appropriate protocol (protocol is disabled or cipher suites are inappropriate)` edite o arquivo `java.security` (que deve estar em `$JRE/lib/security/java.security`, no meu caso estava em `/etc/java-8-openjdk/security/java.security`) e, na opção `jdk.tls.disabledAlgorithms` remova `TLSv1` e `TLSv1.1` da lista.
- O Release 0, quando executado no Windows, pode apresentar ao menos dois problemas: o primeiro é um erro ao tentar subir o webapp no Tomcat, que pode ser resolvido excluindo o arquivo `/WEB-INF/lib/serotonin-timer.jar`, o segundo é a falta das imagens de fundo nos GIFs dinâmicos das Representações Gráficas. O segundo bug será corrigido em breve nas próximas versões.
