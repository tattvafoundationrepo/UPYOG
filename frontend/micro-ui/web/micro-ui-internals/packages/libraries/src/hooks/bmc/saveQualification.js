import { useMutation } from "react-query";
import SchemeService from "../../services/elements/Scheme";

export const useSaveQualification = (config = {}) => {
  return useMutation((data) => SchemeService.saveQualification(data), config);
};

export default useSaveQualification;
