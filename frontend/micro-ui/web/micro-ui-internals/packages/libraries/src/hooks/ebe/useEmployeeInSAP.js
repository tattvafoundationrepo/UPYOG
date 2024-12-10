import { useQuery } from "react-query";
import EBEService from "../../services/elements/EBE";
// const useEmployeeInSAP = (tenantId, filters,executeFlag, config = {}) => {
//   // if (filters.roles) {
//   //   filters.roles = filters.roles.map((role) => role.code).join(",");
//   // }
//   return useQuery(["EMPLOYEE_SEARCH", filters], () => EBEService.employeeSearch(tenantId, filters,executeFlag), config);
// };
export const useEmployeeInSAP = (data , executeFlag=false, config = {}) => {
  return useQuery(["EMPLOYEE_SEARCH", data], () => EBEService.employeeSearch(data,executeFlag),config);
};
export default useEmployeeInSAP;
