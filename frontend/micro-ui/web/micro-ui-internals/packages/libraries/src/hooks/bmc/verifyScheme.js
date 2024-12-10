import { useMutation } from "react-query";
import SchemeService from "../../services/elements/Scheme";

export const useVerifierScheme = (config = {}) => {
  return useMutation((data) => SchemeService.getverifyScheme(data), config);
};

export default useVerifierScheme;
