package com.tcpserver;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class TCPServer {
    public static void main(String[] args) throws Exception {
        Server server = new Server(15000); // Configura o servidor Jetty na porta 15000

        // Crie um ServletContextHandler
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        // Adicione o contexto ao servidor
        server.setHandler(context);

        // Configurar os filtros do Cross-Origin Resource Sharing (CORS) no contexto
        FilterHolder filterHolder = context.addFilter(CrossOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        filterHolder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");

        // Defina o manipulador para o contexto
        context.addServlet(TCPServlet.class, "/keywords/*");

        server.start();
        server.join();
    }
}
