//import { useQuery } from "react-query";
import { useMutation } from "react-query";

export const useWorkFlowTransit = (config = {}) => {
  return useMutation((data) =>Digit.WorkflowService.transit(data), config);
};

// export default useVerifierSchemeDetail;

// export const useVerifierSchemeDetail = (data, config = {}) => {
//   return useQuery(["UserDetails", data], () => SchemeService.getVerifierSchemes(data),config);
// };

// 
// import SchemeService from "../../services/elements/Scheme";

// export const useVerifierSchemeDetail = (config = {}) => {
//   return useMutation((data) => SchemeService.getVerifierSchemes(data), config);
// };

export default useWorkFlowTransit;
