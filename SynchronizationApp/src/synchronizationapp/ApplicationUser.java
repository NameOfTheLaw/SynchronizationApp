/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronizationapp;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/*
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
*/
/**
 * Entity-класс Application_Users в базе данных
 */
@Entity
@Table(name="Application_Users")
public class ApplicationUser implements Serializable {
    
    protected Long id;    
    protected String login;    
    protected String password;

    /**
     * Пустой конструктор
     */
    public ApplicationUser() {}
    
    /**
     * Конструктор по логину и паролю
     * @param login логин пользователя
     * @param password пароль пользователя
     */
    public ApplicationUser (String login, String password) {
        this.login = login;
        this.password = password;
    }
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name="login")
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Column(name="password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (!this.getClass().equals(o.getClass())) {
            return false;
        }
        if (!this.getLogin().equals(((ApplicationUser)o).getLogin())) {
            return false;
        }
        if (!this.getPassword().equals(((ApplicationUser)o).getPassword())) {
            return false;
        }
        return true;
    }
    
}
