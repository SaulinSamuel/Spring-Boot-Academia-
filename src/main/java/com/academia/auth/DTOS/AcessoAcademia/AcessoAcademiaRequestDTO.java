package com.academia.auth.DTOS.AcessoAcademia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcessoAcademiaRequestDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String senha;

}
