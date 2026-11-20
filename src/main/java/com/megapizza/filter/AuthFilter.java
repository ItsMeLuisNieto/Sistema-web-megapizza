package com.megapizza.filter;

import com.megapizza.model.Usuario;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * RNF01: protege las rutas por rol validando el objeto {@code Usuario} de la
 * sesión HTTP. Los cinco roles documentados son CLIENTE, CAJERO, COCINERO,
 * ALMACEN y ADMINISTRADOR.
 */
@WebFilter(urlPatterns = "/*")
public class AuthFilter implements Filter {

    private static final Set<String> RUTAS_PUBLICAS = Set.of(
            "/", "/index.jsp", "/login.jsp", "/auth", "/error.jsp", "/catalogo");

    /** El catálogo es público, así que armar el carrito no debe exigir sesión. */
    private static final Set<String> ACCIONES_CARRITO = Set.of(
            "agregar", "quitar", "fijar", "vaciar");

    /** Rutas de los casos de uso y sus JSP, que no deben abrirse directamente. */
    private static final Map<String, String> RUTA_ROL = Map.ofEntries(
            Map.entry("/pedido", Usuario.ROL_CLIENTE),
            Map.entry("/misPedidos.jsp", Usuario.ROL_CLIENTE),
            Map.entry("/caja", Usuario.ROL_CAJERO),
            Map.entry("/vistaCaja.jsp", Usuario.ROL_CAJERO),
            Map.entry("/cocina", Usuario.ROL_COCINERO),
            Map.entry("/vistaCocina.jsp", Usuario.ROL_COCINERO),
            Map.entry("/almacen", Usuario.ROL_ALMACEN),
            Map.entry("/vistaAlmacen.jsp", Usuario.ROL_ALMACEN),
            Map.entry("/admin", Usuario.ROL_ADMINISTRADOR));

    /** Roles operativos que el administrador puede suplir; CLIENTE queda restringido. */
    private static final Set<String> ROLES_OPERATIVOS = Set.of(
            Usuario.ROL_CAJERO, Usuario.ROL_COCINERO,
            Usuario.ROL_ALMACEN, Usuario.ROL_ADMINISTRADOR);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String ctx = req.getContextPath();
        String ruta = req.getServletPath();
        if (ruta == null || ruta.isEmpty()) {
            ruta = "/";
        }

        if (ruta.startsWith("/static/") || RUTAS_PUBLICAS.contains(ruta)) {
            chain.doFilter(request, response);
            return;
        }

        // CU03: agregar/quitar productos y ver el carrito son operaciones públicas.
        // Confirmar el pedido y ver el historial sí siguen exigiendo rol CLIENTE.
        if ("/pedido".equals(ruta) && esSoloCarrito(req)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession sesion = req.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("usuario");
        if (usuario == null || !usuario.estaActivo()) {
            res.sendRedirect(ctx + "/login.jsp");
            return;
        }

        String rolRequerido = rolRequerido(ruta);
        if (rolRequerido != null
                && !rolRequerido.equals(usuario.getRol())
                && !(Usuario.ROL_ADMINISTRADOR.equals(usuario.getRol())
                        && ROLES_OPERATIVOS.contains(rolRequerido))) {
            res.sendRedirect(ctx + "/index.jsp?error=sin-permiso");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean esSoloCarrito(HttpServletRequest req) {
        String action = req.getParameter("action");
        if ("GET".equalsIgnoreCase(req.getMethod())) {
            return action == null || "ver".equals(action);
        }
        if ("POST".equalsIgnoreCase(req.getMethod())) {
            return action != null
                    && ACCIONES_CARRITO.contains(action.trim().toLowerCase(Locale.ROOT));
        }
        return false;
    }

    private String rolRequerido(String ruta) {
        for (Map.Entry<String, String> entry : RUTA_ROL.entrySet()) {
            String prefijo = entry.getKey();
            if (ruta.equals(prefijo) || ruta.startsWith(prefijo + "/")) {
                return entry.getValue();
            }
        }
        return null;
    }
}
