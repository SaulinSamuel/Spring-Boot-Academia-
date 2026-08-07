package com.academia.auth.Utils;

import java.time.DayOfWeek;

import com.academia.auth.Models.enums.DiasSemana;

public class TradutorDiaDaSemana {
    
    public DiasSemana traduzirDiaSemana(DayOfWeek dayOfWeek) {

        return switch (dayOfWeek) {
            case MONDAY -> DiasSemana.SEGUNDA;
            case TUESDAY -> DiasSemana.TERCA;
            case WEDNESDAY -> DiasSemana.QUARTA;
            case THURSDAY -> DiasSemana.QUINTA;
            case FRIDAY -> DiasSemana.SEXTA;
            case SATURDAY -> DiasSemana.SABADO;
            case SUNDAY -> DiasSemana.DOMINGO;
        };
    }

}
