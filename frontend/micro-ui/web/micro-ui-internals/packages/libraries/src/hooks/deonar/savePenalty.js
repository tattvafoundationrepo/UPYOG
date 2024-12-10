import { useMutation } from "react-query";
import DeonarService from "../../services/elements/deonarService";

export const useSavePenalty = (data, config = {}) => {
  return useMutation((data) => DeonarService.savePenalties(data), config);
};

export default useSavePenalty;
