import { useMutation } from "react-query";
import DeonarService from "../../services/elements/deonarService";

export const useSavePrakingDetail = (data, config = {}) => {
  return useMutation((data) => DeonarService.savePrakingDetail(data), config);
};

export default useSavePrakingDetail;
