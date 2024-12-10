import { useQuery, useMutation } from "react-query";

import DeonarService from "../../services/elements/deonarService";

export const useGetPenalty = (data, config = {}) => {
  return useQuery(["penalty", data], () => DeonarService.getPenalties(data), config);
};

export default useGetPenalty;
