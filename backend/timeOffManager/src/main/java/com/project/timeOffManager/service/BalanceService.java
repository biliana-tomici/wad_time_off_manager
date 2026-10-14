package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.BalanceDto;
import com.project.timeOffManager.dto.command.CreateBalanceCommand;
import com.project.timeOffManager.dto.command.UpdateBalanceCommand;
import com.project.timeOffManager.entity.Balance;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.mapper.BalanceMapper;
import com.project.timeOffManager.repository.BalanceRepository;
import com.project.timeOffManager.repository.UserRepository;
import com.project.timeOffManager.exception.ResourceNotFoundException;
import com.project.timeOffManager.security.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.project.timeOffManager.entity.Request;
import com.project.timeOffManager.repository.RequestRepository;
import jakarta.transaction.Transactional;
import java.util.List;

@Service
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final BalanceMapper balanceMapper;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final AuthorizationService authorizationService;

    @Autowired
    public BalanceService(BalanceRepository balanceRepository,
                          BalanceMapper balanceMapper,
                          UserRepository userRepository,
                          RequestRepository requestRepository,
                          AuthorizationService authorizationService) {
        this.balanceRepository = balanceRepository;
        this.balanceMapper = balanceMapper;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.authorizationService = authorizationService;
    }

    public BalanceDto getBalanceById(Long balanceId) {
        Balance balance = balanceRepository.findBalanceById(balanceId);
        if (balance == null) {
            throw new ResourceNotFoundException("Balance not found with id: " + balanceId);
        }
        authorizationService.checkCanViewBalance(balance);

        return balanceMapper.toDto(balance);
    }

    @Transactional
    public BalanceDto createBalance(CreateBalanceCommand command) {
        authorizationService.checkCanManageBalances();

        User employee = userRepository.findCustomerById(command.employeeId());
        if (employee == null) {
            throw new ResourceNotFoundException("Employee not found with id: " + command.employeeId());
        }

        Balance balance = new Balance(null, employee, command.daysLeft(), command.name(), command.maxDays());
        Balance savedBalance = balanceRepository.save(balance);

        return balanceMapper.toDto(savedBalance);
    }

    @Transactional
    public BalanceDto updateBalance(Long balanceId, UpdateBalanceCommand command) {
        authorizationService.checkCanManageBalances();

        Balance existingBalance = balanceRepository.findBalanceById(balanceId);
        if (existingBalance == null) {
            throw new ResourceNotFoundException("Balance not found with id: " + balanceId);
        }

        if (command.name() != null) {
            existingBalance.setName(command.name());
        }
        if (command.daysLeft() != null) {
            existingBalance.setDaysLeft(command.daysLeft());
        }
        if (command.maxDays() != null) {
            existingBalance.setMaxDays(command.maxDays());
        }

        return balanceMapper.toDto(balanceRepository.save(existingBalance));
    }

    @Transactional
    public boolean deleteBalance(Long balanceId) {
        authorizationService.checkCanManageBalances();

        Balance balance = balanceRepository.findBalanceById(balanceId);
        if (balance == null) {
            throw new ResourceNotFoundException("Balance not found with id: " + balanceId);
        }

        List<Request> requests = requestRepository.findByBalance_Id(balanceId);
        for (Request request : requests) {
            request.setBalance(null);
            requestRepository.save(request);
        }

        balanceRepository.delete(balance);
        return true;
    }

    public Page<BalanceDto> getBalancesByEmployeeId(Long employeeId, Pageable pageable) {
        authorizationService.checkCanViewBalances(employeeId);
        return balanceRepository.findByEmployeeId(employeeId, pageable)
                .map(balanceMapper::toDto);
    }
}
