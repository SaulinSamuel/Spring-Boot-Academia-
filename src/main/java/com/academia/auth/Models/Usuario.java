package com.academia.auth.Models;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.academia.auth.Models.enums.RoleUser;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "usuario"
    )
    private List<Mensalidade> mensalidades;

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "remetente"
    )
    private List<Advertencia> advertenciasEnviadas;

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "destinatario"
    )
    private List<Advertencia> advertenciasRecebidas;

    @OneToOne(
        fetch = FetchType.LAZY,
        mappedBy = "usuario",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private AcessoAcademia acessosAcademia;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private RoleUser role = RoleUser.ROLE_USER;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
            new SimpleGrantedAuthority(role.name())
        );
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
