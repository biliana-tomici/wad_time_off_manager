package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.UserDto;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.mapper.UserMapper;
import com.project.timeOffManager.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserDto getUserById(Long customerId) {

        User user = userRepository.findCustomerById(customerId);

        return userMapper.toDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userMapper.toDtoList(userRepository.findAll());
    }

    @Transactional
    public UserDto createUser(UserDto user) {

        User userEntity = userMapper.toEntity(user);

        if (user.manager() != null) {
            userEntity.setManager(userRepository.findCustomerById(user.manager().id()));
        }

        User savedUser = userRepository.save(userEntity);

        return userMapper.toDto(savedUser);
    }

    public UserDto updateUser(UserDto userDto, Long customerId) {

        User existingUser = userRepository.findCustomerById(customerId);

        if (existingUser == null) {
            return null;
        }

        userMapper.updateUserFromDto(userDto, existingUser);

        return userMapper.toDto(userRepository.save(existingUser));
    }

    public boolean deleteUser(Long customerId) {

        if (userRepository.existsById(customerId)) {
            userRepository.deleteById(customerId);
            return true;
        }

        return false;
    }

    public List<User> getUsersByProjectId(Long projectId) {
        return userRepository.findUsersByProjectId(projectId);
    }
}
