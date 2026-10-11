package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.BalanceDto;
import com.project.timeOffManager.entity.Balance;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.mapper.BalanceMapper;
import com.project.timeOffManager.repository.BalanceRepository;
import com.project.timeOffManager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final BalanceMapper balanceMapper;
    private final UserRepository userRepository;

    @Autowired
    public BalanceService(BalanceRepository balanceRepository, BalanceMapper balanceMapper, UserRepository userRepository) {
        this.balanceRepository = balanceRepository;
        this.balanceMapper = balanceMapper;
        this.userRepository = userRepository;
    }

    public BalanceDto getBalanceById(Long balanceId) {

        Balance balance = balanceRepository.findBalanceById(balanceId);

        return balanceMapper.toDto(balance);
    }

    public BalanceDto createBalance(BalanceDto balanceDto) {

        if (balanceDto.employee() == null || balanceDto.employee().id() == null) {
            return null;
        }

        User employee = userRepository.findCustomerById(balanceDto.employee().id());
        if (employee == null) {
            return null;
        }

        Balance balance = balanceMapper.toEntity(balanceDto);
        balance.setEmployee(employee);
        Balance savedBalance = balanceRepository.save(balance);

        return balanceMapper.toDto(savedBalance);
    }

    public BalanceDto updateBalance(Long balanceId, BalanceDto balanceDto) {
        Balance existingBalance = balanceRepository.findBalanceById(balanceId);

        if (existingBalance == null) {
            return null;
        }

        Balance updatedBalance = balanceMapper.toEntity(balanceDto);
        balanceMapper.updateBalanceFromDto(balanceDto, updatedBalance);
        updatedBalance.setId(balanceId);

        return balanceMapper.toDto(balanceRepository.save(updatedBalance));

    }

    public boolean deleteBalance(Long balanceId) {

        if (balanceRepository.existsById(balanceId)) {
            balanceRepository.deleteById(balanceId);
            return true;
        }

        return false;
    }

    public Page<BalanceDto> getBalancesByEmployeeId(Long employeeId, Pageable pageable) {
        return balanceRepository.findByEmployeeId(employeeId, pageable)
                .map(balanceMapper::toDto);
    }

}
