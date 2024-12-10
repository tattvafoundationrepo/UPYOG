import { useQuery } from "react-query";

import EBEService from "../../services/elements/EBE";

// export const useCommonGet = (data, config = {}) => {
//   return useQuery(["CommonDetails"], () => SchemeService.get(data,config));
//   //return useMutation((data)=>{SchemeService.get(data,Options,config)});
// };
export const useGetEnquiry = (data , executeFlag=false, config = {}) => {
  return useQuery(["Enquiry", data], () => EBEService.getEnquiry(data,executeFlag),config);
};
export default useGetEnquiry;