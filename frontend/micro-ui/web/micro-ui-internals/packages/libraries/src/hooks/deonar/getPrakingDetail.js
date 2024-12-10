import { useQuery, useMutation } from "react-query";

import DeonarService from "../../services/elements/deonarService";

export const useGetPrakingDetail = (data, config = {}) => {
  return useQuery(["ParkingDetails", data], () => DeonarService.getPrakingDetail(data), config);
};

export default useGetPrakingDetail;
