package com.sms.controller.admin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sms.model.User;
import com.sms.util.DBConnection;

/**
 * Servlet for managing teachers
 */
@WebServlet(urlPatterns = { 
    "/admin/teachers", 
    "/admin/teachers/view/*",
    "/admin/teachers/new",
    "/admin/teachers/edit/*",
    "/admin/teachers/delete/*"
})
public class TeacherServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is logged in and is an admin
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        if (!"admin".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String pathInfo = request.getServletPath();
        
        if (pathInfo.equals("/admin/teachers")) {
            // List all teachers
            List<Map<String, Object>> teachers = getAllTeachers();
            request.setAttribute("teachers", teachers);
            request.getRequestDispatcher("/admin/teachers.jsp").forward(request, response);
        } else if (pathInfo.startsWith("/admin/teachers/view/")) {
            // View teacher details
            String idStr = pathInfo.substring("/admin/teachers/view/".length());
            try {
                int id = Integer.parseInt(idStr);
                Map<String, Object> teacher = getTeacherById(id);
                
                if (teacher != null) {
                    request.setAttribute("teacher", teacher);
                    
                    // Get teacher's courses
                    List<Map<String, Object>> teacherCourses = getTeacherCourses(id);
                    request.setAttribute("teacherCourses", teacherCourses);
                    
                    // Get teacher statistics
                    Map<String, Object> teacherStats = getTeacherStats(id);
                    request.setAttribute("teacherStats", teacherStats);
                    
                    request.getRequestDispatcher("/admin/teacher-view.jsp").forward(request, response);
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin/teachers");
                }
            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/admin/teachers");
            }
        } else if (pathInfo.equals("/admin/teachers/new")) {
            // Show form to create a new teacher
            // Get available courses for assignment
            List<Map<String, Object>> courses = getAllCourses();
            request.setAttribute("courses", courses);
            request.getRequestDispatcher("/admin/teacher-form.jsp").forward(request, response);
        } else if (pathInfo.startsWith("/admin/teachers/edit/")) {
            // Show form to edit an existing teacher
            String idStr = pathInfo.substring("/admin/teachers/edit/".length());
            try {
                int id = Integer.parseInt(idStr);
                Map<String, Object> teacher = getTeacherById(id);
                
                if (teacher != null) {
                    request.setAttribute("teacher", teacher);
                    
                    // Get teacher's courses
                    List<Map<String, Object>> teacherCourses = getTeacherCourses(id);
                    request.setAttribute("teacherCourses", teacherCourses);
                    
                    // Get all courses for assignment
                    List<Map<String, Object>> courses = getAllCourses();
                    request.setAttribute("courses", courses);
                    
                    request.getRequestDispatcher("/admin/teacher-form.jsp").forward(request, response);
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin/teachers");
                }
            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/admin/teachers");
            }
        } else if (pathInfo.startsWith("/admin/teachers/delete/")) {
            // Delete a teacher
            String idStr = pathInfo.substring("/admin/teachers/delete/".length());
            try {
                int id = Integer.parseInt(idStr);
                deleteTeacher(id);
                
                response.sendRedirect(request.getContextPath() + "/admin/teachers?message=Teacher+deleted+successfully");
            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/admin/teachers");
            }
        } else {
            // Redirect to teachers list for any invalid URL
            response.sendRedirect(request.getContextPath() + "/admin/teachers");
        }
    }
    
    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is logged in and is an admin
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        if (!"admin".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String action = request.getParameter("action");
        
        if ("create".equals(action)) {
            // Create a new teacher
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String email = request.getParameter("email");
            String telephone = request.getParameter("telephone");
            String qualification = request.getParameter("qualification");
            String experienceStr = request.getParameter("experience");
            String password = request.getParameter("password");
            
            int experience = 0;
            try {
                experience = Integer.parseInt(experienceStr);
            } catch (NumberFormatException e) {
                // Use default value
            }
            
            if (firstName != null && !firstName.trim().isEmpty() && 
                lastName != null && !lastName.trim().isEmpty() && 
                email != null && !email.trim().isEmpty()) {
                
                // Create teacher
                int teacherId = createTeacher(firstName, lastName, email, telephone, qualification, experience);
                
                if (teacherId > 0) {
                    // Create user account for teacher
                    createUserAccount(email, password, "Teacher", teacherId);
                    
                    // Handle course assignments
                    String[] courseIds = request.getParameterValues("courses");
                    if (courseIds != null) {
                        for (String courseId : courseIds) {
                            try {
                                int cId = Integer.parseInt(courseId);
                                assignCourseToTeacher(teacherId, cId);
                            } catch (NumberFormatException e) {
                                // Skip invalid course ID
                            }
                        }
                    }
                    
                    response.sendRedirect(request.getContextPath() + "/admin/teachers?message=Teacher+added+successfully");
                    return;
                }
            }
            
            // If we get here, something went wrong
            response.sendRedirect(request.getContextPath() + "/admin/teachers/new?error=Failed+to+create+teacher");
        } else if ("update".equals(action)) {
            // Update an existing teacher
            String idStr = request.getParameter("id");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String email = request.getParameter("email");
            String telephone = request.getParameter("telephone");
            String qualification = request.getParameter("qualification");
            String experienceStr = request.getParameter("experience");
            
            int experience = 0;
            try {
                experience = Integer.parseInt(experienceStr);
            } catch (NumberFormatException e) {
                // Use default value
            }
            
            if (idStr != null && !idStr.trim().isEmpty() && 
                firstName != null && !firstName.trim().isEmpty() && 
                lastName != null && !lastName.trim().isEmpty() && 
                email != null && !email.trim().isEmpty()) {
                
                try {
                    int id = Integer.parseInt(idStr);
                    
                    // Update teacher
                    updateTeacher(id, firstName, lastName, email, telephone, qualification, experience);
                    
                    // Update user account email
                    updateUserEmail(id, email);
                    
                    // Handle course assignments
                    // First, remove all existing assignments
                    removeTeacherCourses(id);
                    
                    // Then add new assignments
                    String[] courseIds = request.getParameterValues("courses");
                    if (courseIds != null) {
                        for (String courseId : courseIds) {
                            try {
                                int cId = Integer.parseInt(courseId);
                                assignCourseToTeacher(id, cId);
                            } catch (NumberFormatException e) {
                                // Skip invalid course ID
                            }
                        }
                    }
                    
                    response.sendRedirect(request.getContextPath() + "/admin/teachers?message=Teacher+updated+successfully");
                    return;
                } catch (NumberFormatException e) {
                    // Invalid ID
                }
            }
            
            // If we get here, something went wrong
            response.sendRedirect(request.getContextPath() + "/admin/teachers?error=Failed+to+update+teacher");
        } else {
            // Unknown action
            response.sendRedirect(request.getContextPath() + "/admin/teachers");
        }
    }
    
    /**
     * Get all teachers
     * @return list of teacher data
     */
    private List<Map<String, Object>> getAllTeachers() {
        List<Map<String, Object>> teachers = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM Teachers ORDER BY LastName, FirstName";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Map<String, Object> teacher = new HashMap<>();
                    teacher.put("id", rs.getInt("TeacherId"));
                    teacher.put("firstName", rs.getString("FirstName"));
                    teacher.put("lastName", rs.getString("LastName"));
                    teacher.put("email", rs.getString("Email"));
                    teacher.put("telephone", rs.getString("Telephone"));
                    teacher.put("qualification", rs.getString("Qualification"));
                    teacher.put("experience", rs.getInt("Experience"));
                    
                    teachers.add(teacher);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return teachers;
    }
    
    /**
     * Get teacher by ID
     * @param id teacher ID
     * @return teacher data
     */
    private Map<String, Object> getTeacherById(int id) {
        Map<String, Object> teacher = null;
        Connection conn = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM Teachers WHERE TeacherId = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        teacher = new HashMap<>();
                        teacher.put("id", rs.getInt("TeacherId"));
                        teacher.put("firstName", rs.getString("FirstName"));
                        teacher.put("lastName", rs.getString("LastName"));
                        teacher.put("email", rs.getString("Email"));
                        teacher.put("telephone", rs.getString("Telephone"));
                        teacher.put("qualification", rs.getString("Qualification"));
                        teacher.put("experience", rs.getInt("Experience"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return teacher;
    }
    
    /**
     * Get courses assigned to a teacher
     * @param teacherId teacher ID
     * @return list of courses
     */
    private List<Map<String, Object>> getTeacherCourses(int teacherId) {
        List<Map<String, Object>> courses = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT c.* FROM Courses c WHERE c.TeacherId = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, teacherId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> course = new HashMap<>();
                        course.put("id", rs.getInt("CourseId"));
                        course.put("courseName", rs.getString("CourseName"));
                        course.put("description", rs.getString("Description"));
                        
                        courses.add(course);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return courses;
    }
    
    /**
     * Get all courses
     * @return list of all courses
     */
    private List<Map<String, Object>> getAllCourses() {
        List<Map<String, Object>> courses = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM Courses ORDER BY CourseName";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Map<String, Object> course = new HashMap<>();
                    course.put("id", rs.getInt("CourseId"));
                    course.put("courseName", rs.getString("CourseName"));
                    course.put("description", rs.getString("Description"));
                    course.put("teacherId", rs.getInt("TeacherId"));
                    
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return courses;
    }
    
    /**
     * Get teacher statistics
     * @param teacherId teacher ID
     * @return teacher statistics
     */
    private Map<String, Object> getTeacherStats(int teacherId) {
        Map<String, Object> stats = new HashMap<>();
        Connection conn = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Count students enrolled in teacher's courses
            String sql = "SELECT COUNT(DISTINCT sc.student_id) AS total_students " +
                         "FROM student_courses sc " +
                         "JOIN Courses c ON sc.course_id = c.CourseId " +
                         "WHERE c.TeacherId = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, teacherId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.put("totalStudents", rs.getInt("total_students"));
                    }
                }
            }
            
            // Get average student performance in teacher's courses
            sql = "SELECT AVG(m.Marks) AS avg_performance " +
                  "FROM Marks m " +
                  "JOIN Courses c ON m.CourseId = c.CourseId " +
                  "WHERE c.TeacherId = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, teacherId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        double avgPerformance = rs.getDouble("avg_performance");
                        if (!rs.wasNull()) {
                            stats.put("avgPerformance", String.format("%.1f%%", avgPerformance));
                        }
                    }
                }
            }
            
            // For simplicity, we'll use a placeholder for classes per week
            stats.put("classesPerWeek", "N/A");
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return stats;
    }
    
    /**
     * Create a new teacher
     * @param firstName the teacher's first name
     * @param lastName the teacher's last name
     * @param email the teacher's email
     * @param telephone the teacher's phone number
     * @param qualification the teacher's qualification
     * @param experience the teacher's years of experience
     * @return the new teacher ID, or -1 if creation failed
     */
    private int createTeacher(String firstName, String lastName, String email, String telephone, 
                              String qualification, int experience) {
        Connection conn = null;
        int teacherId = -1;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO Teachers (FirstName, LastName, Email, Telephone, Qualification, Experience) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, email);
                stmt.setString(4, telephone);
                stmt.setString(5, qualification);
                stmt.setInt(6, experience);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            teacherId = generatedKeys.getInt(1);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return teacherId;
    }
    
    /**
     * Update an existing teacher
     * @param id the teacher ID
     * @param firstName the teacher's first name
     * @param lastName the teacher's last name
     * @param email the teacher's email
     * @param telephone the teacher's phone number
     * @param qualification the teacher's qualification
     * @param experience the teacher's years of experience
     * @return true if update successful, false otherwise
     */
    private boolean updateTeacher(int id, String firstName, String lastName, String email, 
                                String telephone, String qualification, int experience) {
        Connection conn = null;
        boolean success = false;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE Teachers SET FirstName = ?, LastName = ?, Email = ?, " +
                         "Telephone = ?, Qualification = ?, Experience = ? WHERE TeacherId = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, email);
                stmt.setString(4, telephone);
                stmt.setString(5, qualification);
                stmt.setInt(6, experience);
                stmt.setInt(7, id);
                
                int rowsAffected = stmt.executeUpdate();
                success = rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return success;
    }
    
    /**
     * Delete a teacher
     * @param id the ID of the teacher to delete
     * @return true if deletion successful, false otherwise
     */
    private boolean deleteTeacher(int id) {
        Connection conn = null;
        boolean success = false;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            try {
                // First update any courses assigned to this teacher
                String updateCoursesSQL = "UPDATE Courses SET TeacherId = NULL WHERE TeacherId = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateCoursesSQL)) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
                
                // Delete the teacher
                String deleteTeacherSQL = "DELETE FROM Teachers WHERE TeacherId = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteTeacherSQL)) {
                    stmt.setInt(1, id);
                    int rowsAffected = stmt.executeUpdate();
                    success = rowsAffected > 0;
                }
                
                // Delete associated user account
                String deleteUserSQL = "DELETE FROM Users WHERE UserType = 'Teacher' AND UserID = (SELECT user_id FROM Students WHERE student_id = ?)";
                try (PreparedStatement stmt = conn.prepareStatement(deleteUserSQL)) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return success;
    }
    
    /**
     * Create a user account for a teacher
     * @param username the username (email)
     * @param password the password
     * @param userType the user type (Teacher)
     * @param associatedId the teacher ID
     * @return true if creation successful, false otherwise
     */
    private boolean createUserAccount(String username, String password, String userType, int associatedId) {
        Connection conn = null;
        boolean success = false;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO Users (Username, Password, UserType) VALUES (?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password); // In a real app, password should be hashed
                stmt.setString(3, userType);
                
                int rowsAffected = stmt.executeUpdate();
                success = rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return success;
    }
    
    /**
     * Update user account email for a teacher
     * @param teacherId the teacher ID
     * @param email the new email
     * @return true if update successful, false otherwise
     */
    private boolean updateUserEmail(int teacherId, String email) {
        Connection conn = null;
        boolean success = false;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE Users SET Username = ? WHERE UserType = 'Teacher' AND UserID = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                stmt.setInt(2, teacherId);
                
                int rowsAffected = stmt.executeUpdate();
                success = rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return success;
    }
    
    /**
     * Assign a course to a teacher
     * @param teacherId the teacher ID
     * @param courseId the course ID
     * @return true if assignment successful, false otherwise
     */
    private boolean assignCourseToTeacher(int teacherId, int courseId) {
        Connection conn = null;
        boolean success = false;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE Courses SET TeacherId = ? WHERE CourseId = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, teacherId);
                stmt.setInt(2, courseId);
                
                int rowsAffected = stmt.executeUpdate();
                success = rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return success;
    }
    
    /**
     * Remove all course assignments for a teacher
     * @param teacherId the teacher ID
     * @return true if removal successful, false otherwise
     */
    private boolean removeTeacherCourses(int teacherId) {
        Connection conn = null;
        boolean success = false;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE Courses SET TeacherId = NULL WHERE TeacherId = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, teacherId);
                
                stmt.executeUpdate();
                success = true; // Consider successful even if no rows were updated
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return success;
    }
} 