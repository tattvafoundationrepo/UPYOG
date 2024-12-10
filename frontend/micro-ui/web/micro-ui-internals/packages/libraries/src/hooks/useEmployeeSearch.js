import { useQuery } from "react-query";

const useEmployeeSearch = (tenantId, filters,executeFlag, config = {}) => {
  if (filters.roles) {
    filters.roles = filters.roles.map((role) => role.code).join(",");
  }
  return useQuery(["EMPLOYEE_SEARCH", filters], () => Digit.UserService.employeeSearch(tenantId, filters,executeFlag), config);
};

export default useEmployeeSearch;
