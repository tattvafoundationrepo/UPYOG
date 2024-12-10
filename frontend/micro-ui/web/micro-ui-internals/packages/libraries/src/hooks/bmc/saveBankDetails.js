import { useMutation } from "react-query";
import SchemeService from "../../services/elements/Scheme";

export const useSaveBankDetails = (config = {}) => {
  return useMutation((data) => SchemeService.saveBankDetails(data), config);
};

export default useSaveBankDetails;
