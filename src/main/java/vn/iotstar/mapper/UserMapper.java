package vn.iotstar.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // map roleName từ roles (lấy role đầu tiên)
    @Mapping(target = "roleName", expression = "java(firstRoleName(user))")
    UserDTO toDto(User user);

    default String firstRoleName(User user) {
        if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) return null;
        Role r = user.getRoles().iterator().next();
        return r == null ? null : r.getName();
    }
}