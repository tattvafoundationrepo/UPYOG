// // src/hooks/useBMCCommon.js
// import { useQuery, useMutation, useQueryClient } from "react-query";
// import { useDispatch } from "react-redux";
// import DeonarService from "../../services/elements/deonarService";
// import SchemeService from "../../services/elements/Scheme";


// const useBMCCommon = () => {
//   const queryClient = useQueryClient();

//   // Fetching data
//   const fetchBMCCommon = (config = {}) => {
//     return useQuery(["BMCDetails", config], () =>
//       SchemeService.getAllinboxApplications(data), {
//       ...config,
//       onSuccess: (data) => {
//         console.log(data, "BMC All Applications")
//       },
//     });
//   };

//   return {
//     fetchBMCCommon,
//   };
// };

// export default useBMCCommon;





import { useQuery } from "react-query";

import SchemeService from "../../services/elements/Scheme";

export const useBMCDataCommon = (data, config = {}) => {
  return useQuery(["ApplicationStatus", data], () => SchemeService.getAllinboxApplications(data), config);
};

export default useBMCDataCommon;
