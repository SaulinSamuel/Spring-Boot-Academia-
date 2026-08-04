package com.academia.auth.DTOS.Advertencia;

import com.academia.auth.Models.enums.AdvertenciaStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdvertenciaRequestDTO {
    
    @NotBlank
    @Size(min = 9, max = 400)
    private String mensagem;

    @NotNull
    private AdvertenciaStatus nivel;

}
