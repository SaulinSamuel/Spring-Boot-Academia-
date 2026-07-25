package com.academia.auth.DTOS.Usuario;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDeletarDTO {
    
    @NotBlank(message = "Senha é obrigatória!")
    private String senhaAtual;

}
