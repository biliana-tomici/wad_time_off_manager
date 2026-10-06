package com.project.timeOffManager.mapper;

import com.project.timeOffManager.dto.BalanceDto;
import com.project.timeOffManager.entity.Balance;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BalanceMapper {

    Balance toEntity(BalanceDto balanceDto);

    BalanceDto toDto(Balance balance);

    List<BalanceDto> toDtoList(List<Balance> balances);

    List<Balance> toEntityList(List<BalanceDto> balanceDtos);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBalanceFromDto(BalanceDto balanceDto, @MappingTarget Balance balance);
}
