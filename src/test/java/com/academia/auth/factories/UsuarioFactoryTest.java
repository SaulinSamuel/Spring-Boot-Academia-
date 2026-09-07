package com.academia.auth.factories;

import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;

public class UsuarioFactoryTest {
    
    public Usuario criarUsuario() {

        return Usuario.builder()
            .id(1L)
            .nome("teste")
            .email("teste@gmail.com")
            .telefone("(94) 94724-8524")
            .senha("091812")
            .role(RoleUser.ROLE_USER)
        .build();
    }

}
