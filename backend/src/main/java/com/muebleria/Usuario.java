package com.muebleria;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Usuario que puede entrar al panel (en la práctica, solo el dueño).
 * La contraseña nunca se guarda tal cual: se guarda el hash BCrypt.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usuario;

    @JsonIgnore // por las dudas, que nunca salga en una respuesta
    private String claveHash;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getClaveHash() { return claveHash; }
    public void setClaveHash(String claveHash) { this.claveHash = claveHash; }
}
