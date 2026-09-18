package com.academia.auth.Events;

import java.time.LocalDate;

public record AulaConfirmadaEvent(
    Long aulaId,
    String nomeAula,
    LocalDate dataAula
) {
    
}
