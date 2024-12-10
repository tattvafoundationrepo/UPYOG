import { useMutation } from "react-query";
import EBEService from "../../services/elements/EBE";

export const useEBEProcessEnquiry = (config = {}) => {
  return useMutation((data) => EBEService.ProcessEnquiry(data), config);
};

export default useEBEProcessEnquiry;