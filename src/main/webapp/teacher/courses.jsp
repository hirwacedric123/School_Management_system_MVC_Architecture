<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Courses | Teacher Dashboard</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome for icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/teacher-styles.css">
</head>
<body>
    <!-- Sidebar -->
    <jsp:include page="/WEB-INF/includes/teacher-sidebar.jsp">
        <jsp:param name="active" value="courses"/>
    </jsp:include>

    <!-- Main Content -->
    <div class="col-md-10 main-content">
        <div class="container-fluid">
            <div class="row mb-4">
                <div class="col">
                    <h2><i class="fas fa-book me-2"></i> My Courses</h2>
                    <p class="text-muted">Manage your assigned courses and student progress</p>
                </div>
            </div>

            <!-- Alert messages -->
            <c:if test="${not empty message}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    ${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    ${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <!-- Search and Filters -->
            <div class="row mb-4">
                <div class="col-md-6">
                    <div class="input-group">
                        <input type="text" class="form-control" placeholder="Search courses..." id="searchInput">
                        <button class="btn btn-primary" type="button">
                            <i class="fas fa-search"></i>
                        </button>
                    </div>
                </div>
            </div>

            <!-- Course Cards -->
            <div class="row g-4" id="courseContainer">
                <c:forEach var="course" items="${courses}">
                    <div class="col-md-4 course-item">
                        <div class="card course-card">
                            <div class="position-relative">
                                <img src="${pageContext.request.contextPath}/images/course-${course.courseId % 5 + 1}.jpg" class="card-img-top" alt="${course.courseName}" style="height: 160px; object-fit: cover;">
                                <div class="student-count" title="${course.studentCount} Students">
                                    ${course.studentCount}
                                </div>
                            </div>
                            <div class="card-body d-flex flex-column">
                                <h5 class="card-title">${course.courseName}</h5>
                                <p class="card-text text-muted small">
                                    <c:choose>
                                        <c:when test="${course.description.length() > 100}">
                                            ${course.description.substring(0, 100)}...
                                        </c:when>
                                        <c:otherwise>
                                            ${course.description}
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                                <div class="mt-auto pt-3">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <a href="${pageContext.request.contextPath}/course/view?id=${course.courseId}" class="btn btn-primary btn-sm">
                                            <i class="fas fa-eye me-1"></i> View Details
                                        </a>
                                        <div>
                                            <a href="${pageContext.request.contextPath}/grading/course?id=${course.courseId}" class="btn btn-outline-secondary btn-sm">
                                                <i class="fas fa-star"></i>
                                            </a>
                                            <a href="${pageContext.request.contextPath}/material/course?id=${course.courseId}" class="btn btn-outline-secondary btn-sm">
                                                <i class="fas fa-file"></i>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>

                <c:if test="${empty courses}">
                    <div class="col-12">
                        <div class="alert alert-info">
                            <i class="fas fa-info-circle me-2"></i> You don't have any assigned courses yet.
                        </div>
                    </div>
                </c:if>
            </div>
        </div>
    </div>

    <!-- Bootstrap JS and dependencies -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Search functionality
        document.getElementById('searchInput').addEventListener('keyup', function() {
            let searchValue = this.value.toLowerCase();
            let courses = document.getElementsByClassName('course-item');
            
            for (let i = 0; i < courses.length; i++) {
                let courseTitle = courses[i].getElementsByClassName('card-title')[0].textContent.toLowerCase();
                let courseDesc = courses[i].getElementsByClassName('card-text')[0].textContent.toLowerCase();
                
                if (courseTitle.includes(searchValue) || courseDesc.includes(searchValue)) {
                    courses[i].style.display = "";
                } else {
                    courses[i].style.display = "none";
                }
            }
        });
    </script>
</body>
</html> 