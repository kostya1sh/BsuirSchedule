package by.bsuir.schedule.model;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by Лол on 05.08.2015.
 */
public class Employee implements Serializable{
    private String firstName;
    private String lastName;
    private String middleName;
    private Long id;
    private String department;
    private transient Bitmap photo;
    private String photoURL;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String link) {
        this.photoURL = link;
    }
}
