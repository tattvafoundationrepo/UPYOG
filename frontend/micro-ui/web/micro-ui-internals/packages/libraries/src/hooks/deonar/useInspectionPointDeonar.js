import { useMutation } from "react-query";
import DeonarService from "../../services/elements/deonarService";

export const useInspectionPointSave = (config = {}) => {
  return useMutation((data) => DeonarService.saveInspectionPoint(data), config);
};

export default useInspectionPointSave;
