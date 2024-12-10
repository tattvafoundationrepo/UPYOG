import { useMutation } from "react-query";
import DeonarService from "../../services/elements/deonarService";

export const useGetInspectionPointData = (data, config = {}) => {
  return useMutation((data) => DeonarService.getInspectionPoint(data), config);
};

export default useGetInspectionPointData;
