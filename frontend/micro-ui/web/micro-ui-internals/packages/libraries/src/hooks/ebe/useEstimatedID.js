import { useQuery } from "react-query";

import EBEService from "../../services/elements/EBE";

// export const useCommonGet = (data, config = {}) => {
//   return useQuery(["CommonDetails"], () => SchemeService.get(data,config));
//   //return useMutation((data)=>{SchemeService.get(data,Options,config)});
// };
export const useEstimatedID = (data , executeFlag=false, config = {}) => {
  return useQuery(["EstimatedID", data], () => EBEService.getEstimatedID(data,executeFlag),config);
};
export default useEstimatedID;