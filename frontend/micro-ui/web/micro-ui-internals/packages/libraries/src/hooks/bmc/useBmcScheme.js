import { useQuery } from "react-query";

import SchemeService from "../../services/elements/Scheme";

export const useBmcScheme = (data, config = {}) => {
  return useQuery(["ApplicationStatus", data], () => SchemeService.selectBmcScheme(data), config);
};

export default useBmcScheme;
