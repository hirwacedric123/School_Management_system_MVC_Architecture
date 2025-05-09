package com.sms.model;

/**
 * Course model class
 */
public class Course {
    private int courseId;
    private String courseName;
    private String description;
    private int teacherId;
    private transient int studentCount; // Transient field, not stored in database
    
    /**
     * Default constructor
     */
    public Course() {
    }
    
    /**
     * Constructor with all fields except courseId
     * @param courseName the course name
     * @param description the description
     * @param teacherId the teacher id
     */
    public Course(String courseName, String description, int teacherId) {
        this.courseName = courseName;
        this.description = description;
        this.teacherId = teacherId;
    }
    
    /**
     * Constructor with all fields
     * @param courseId the course id
     * @param courseName the course name
     * @param description the description
     * @param teacherId the teacher id
     */
    public Course(int courseId, String courseName, String description, int teacherId) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.description = description;
        this.teacherId = teacherId;
    }
    
    // Getters and setters
    
    public int getCourseId() {
        return courseId;
    }
    
    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }
    
    /**
     * Get the number of students enrolled in this course
     * @return the student count
     */
    public int getStudentCount() {
        return studentCount;
    }
    
    /**
     * Set the number of students enrolled in this course
     * @param studentCount the student count
     */
    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }
    
    @Override
    public String toString() {
        return "Course [courseId=" + courseId + ", courseName=" + courseName + ", teacherId=" + teacherId + "]";
    }
} 