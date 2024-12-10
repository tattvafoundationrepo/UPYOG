import { useQuery } from "react-query";

import SchemeService from "../../services/elements/Scheme";

export const useBMCApplicationStatus = (data, config = {}) => {
  return useQuery(["ApplicationStatus", data], () => SchemeService.getAllApplications(data), config);
};

export default useBMCApplicationStatus;
