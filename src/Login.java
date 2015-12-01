

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.genericdao.ConnectionPool;
import org.genericdao.DAOException;
import org.genericdao.RollbackException;
import org.mybeans.form.FormBeanException;
import org.mybeans.form.FormBeanFactory;


public class Login extends HttpServlet {

    private static final long serialVersionUID = 1L;

//    private FavoriteDAO favoriteDAO;
    private UserDAO userDAO;
    
    private FormBeanFactory<LoginForm> loginFormFactory = FormBeanFactory.getInstance(LoginForm.class);

    public void init() throws ServletException {
        ServletContext context = getServletContext();
        String jdbcDriverName = context.getInitParameter("jdbcDriverName");
        String jdbcURL = context.getInitParameter("jdbcURL");

        try {
            ConnectionPool connectionPool = new ConnectionPool(jdbcDriverName,
                    jdbcURL);
            userDAO = new UserDAO(connectionPool, "haoruiw_user");
//            favoriteDAO = new FavoriteDAO(connectionPool, "haoruiw_favorite");
        } catch (DAOException e) {
            throw new ServletException(e);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("email") != null) {
            response.sendRedirect("HW3");
            return;
        }

        List<String> errors = new ArrayList<String>();
        request.setAttribute("errors", errors);

        LoginForm form = null;
        try {
            form = loginFormFactory.create(request);
            request.setAttribute("form", form);

            if (!form.isPresent()) {
                RequestDispatcher d = request.getRequestDispatcher("login.jsp");
                d.forward(request, response);
                return;
            }

            errors.addAll(form.getValidationErrors());
            if (errors.size() != 0) {
                RequestDispatcher d = request.getRequestDispatcher("login.jsp");
                d.forward(request, response);
                return;
            }

            UserBean user = userDAO.getUser(form.getEmail());
            if (user == null) {
                errors.add("No such user");
                RequestDispatcher d = request.getRequestDispatcher("login.jsp");
                d.forward(request, response);
                return;
            }

            if (!form.getPassword().equals(user.getPassword())) {
                errors.add("Incorrect password");
                RequestDispatcher d = request.getRequestDispatcher("login.jsp");
                d.forward(request, response);
                return;
            }

            session.setAttribute("email", user);
            response.sendRedirect("HW3");

        } catch (RollbackException e) {
            errors.add(e.getMessage());
            RequestDispatcher d = request.getRequestDispatcher("error.jsp");
            d.forward(request, response);
        } catch (FormBeanException e) {
            errors.add(e.getMessage());
            RequestDispatcher d = request.getRequestDispatcher("error.jsp");
            d.forward(request, response);
        }
    }
}