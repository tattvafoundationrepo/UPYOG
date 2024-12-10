import { useQuery } from "react-query";
import EBEService from "../../services/elements/EBE";

// export const useCommonGet = (data, config = {}) => {
//   return useQuery(["CommonDetails"], () => SchemeService.get(data,config));
//   //return useMutation((data)=>{SchemeService.get(data,Options,config)});
// };
export const useENQCount = (data, config = {}) => {
  return useQuery(["EBECOUNT", data], () => EBEService.getEBECount(data),config);
};
export default useENQCount;