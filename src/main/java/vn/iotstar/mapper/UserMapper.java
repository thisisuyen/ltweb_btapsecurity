package vn.iotstar.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  @Mapping(target = "roleId", source = "role.id")
  @Mapping(target = "roleName", source = "role.name")
  @Mapping(target = "productCount",
      expression = "java(user.getProducts() == null ? 0 : user.getProducts().size())")
  UserDTO toDto(User user);

  // Khi convert DTO -> Entity: role và products sẽ set ở service => ignore
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "products", ignore = true)
  User toEntity(UserDTO dto);

  // Update entity từ dto (không overwrite null)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "products", ignore = true)
  void updateEntityFromDto(UserDTO dto, @MappingTarget User entity);
}