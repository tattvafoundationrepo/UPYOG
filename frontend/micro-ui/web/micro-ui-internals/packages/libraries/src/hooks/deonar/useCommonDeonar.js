// src/hooks/useDeonarCommon.js
import { useQuery, useMutation, useQueryClient } from "react-query";
import { useDispatch } from "react-redux";
import DeonarService from "../../services/elements/deonarService";
import {
  fetchDeonarDetailsSuccess,
  fetchStakeholderDetailsSuccess,
  saveDeonarDetailsFailure,
  saveDeonarDetailsSuccess,
  fetchDeonarEntryFeeDetailsSuccess,
} from "../../../../modules/deonar/src/redux/actions/securityDataActions";

const useDeonarCommon = () => {
  const queryClient = useQueryClient();
  const dispatch = useDispatch();

  // Fetching data
  const fetchDeonarCommon = (data, config = {}) => {
    return useQuery(["DeonarDetails", data], () => DeonarService.get(data), {
      ...config,
      onSuccess: (data) => {
        dispatch(fetchDeonarDetailsSuccess(data));
      },
    });
  };

  const searchDeonarCommon = (data, config = {}) => {
    return useQuery(["StakeholderDetails", data], () => DeonarService.getStakeholders(data), {
      ...config,
      onSuccess: (data) => {
        const filteredData = data.CommonDetails.filter((item) => item.key !== "animal");
        dispatch(fetchStakeholderDetailsSuccess(filteredData));
      },
    });
  };

  // const searchDeonarCommon = (data, config = {}) => {
  //   return useQuery(
  //     ['StakeholderDetails', data],
  //     () => DeonarService.getStakeholders(data),
  //     {
  //       ...config,
  //       onSuccess: (data) => {
  //         dispatch(fetchStakeholderDetailsSuccess(data));
  //       },
  //     }
  //   );
  // };
  // Saving data
  const { mutate: saveDeonarDetails, isLoading: isSaving, isError: saveError } = useMutation((data) => DeonarService.saveDeonarUserDetails(data), {
    onSuccess: () => {
      dispatch(saveDeonarDetailsSuccess());
      queryClient.invalidateQueries("DeonarValidateDetails");
    },
    onError: (error) => {
      dispatch(saveDeonarDetailsFailure(error.message));
      console.error("Error saving Deonar details:", error);
    },
  });

  const fetchEntryFeeDetailsbyUUID = (securityCheckCriteria, config = {}) => {
    return useQuery(
      ["fetchEntryFeeDetails", securityCheckCriteria], // Ensure key includes dynamic part
      () => DeonarService.searchDeonarDetails({ SecurityCheckCriteria: securityCheckCriteria }),
      {
        ...config,
        onSuccess: (data) => {
          dispatch(fetchDeonarEntryFeeDetailsSuccess(data));
        },
        onError: (error) => {
          console.error("Error fetching Deonar Entry fee details:", error);
        },
        staleTime: 0, 
        cacheTime: 0, 
        refetchOnWindowFocus: true, 
      }
    );
  };
  

  const { mutate: saveStablingDetails } = useMutation(
    (data) => DeonarService.saveStablingPoint(data),
    {
      onSuccess: async () => {
        await queryClient.invalidateQueries("DeonarStablingDetails");
        await queryClient.refetchQueries("DeonarStablingDetails", {
          active: true,
          exact: true
        });
      },

      onError: (error) => {
        console.error("Error saving Deonar details:", error);
        throw error; 
      },
    }
  );

  // const fetchTradingList = (data, config = {}) => {
  //   return useQuery(["TradingFee", data], () => DeonarService.getTradingList(data), {
  //     ...config,
  //     onSuccess: (data) => {
  //       console.log(data, "Trading collection data");
  //     },
  //     enabled: true,
  //   });
  // };

  // const fetchStablingList = (data, config = {}) => {
  //   return useQuery(["StablingFee", data], () => DeonarService.getStablingList(data), {
  //     ...config,
  //     onSuccess: (data) => {
  //       console.log(data, "Stabling collection data");
  //     },
  //     enabled: true,
  //   });
  // };

  // const fetchDawanwalaList = (data, config = {}) => {
  //   return useQuery(["fetchDawanwalaList", data], () => DeonarService.getDawanwalaList(data, config));
  // };

  const fetchTradingList = (data, options = {}) => {
    const { 
      enabled = true,
      executeOnLoad = false,
      executeOnRadioSelect = false
    } = options;

    return useQuery(["TradingFee", data], () => DeonarService.getTradingList(data), {
      enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...options,
      onSuccess: (data) => {
        console.log(data, "Trading collection data");
        options.onSuccess && options.onSuccess(data);
      },
    });
  };

  // Similar modifications for other fetch methods...
  const fetchStablingList = (data, options = {}) => {
    const { 
      enabled = true,
      executeOnLoad = false,
      executeOnRadioSelect = false
    } = options;

    return useQuery(["StablingFee", data], () => DeonarService.getStablingList(data), {
      enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...options,
      onSuccess: (data) => {
        console.log(data, "Stabling collection data");
        options.onSuccess && options.onSuccess(data);
      },
    });
  };

  const fetchDawanwalaList = (data, config = {}) => {
    return useQuery(["DeonarStablingDetails", data], () => DeonarService.getDawanwalaList(data), {
      ...config,
      onSuccess: (data) => {
        console.log(data, "Gawal collection data");
      },
      enabled: true,
    });
  };

  const fetchHelkariList = (data, config = {}) => {
    return useQuery(["fetchHelkariList", data], () => DeonarService.getHelkariList(data, config));
  };

  const fetchGatePassSearchData = (data, config = {}) => {
    return useMutation((data) => DeonarService.getGatePassSearchData(data), config);
  };

  const saveGatePassData = (data, config = {}) => {
    return useMutation((data) => DeonarService.saveGatePassData(data), config);
  };

  const saveSlaughterListData = (data, config = {}) => {
    return useMutation((data) => DeonarService.saveSlaughterListData(data), config);
  };


  const fetchSlaughterUnit = (data, options = {}) => {
    const { 
      enabled = true,
      executeOnLoad = false,
      executeOnRadioSelect = false
    } = options;
    return useQuery(["fetchSlaughterUnit", data], () => DeonarService.getSlaughterUnit(data), {
      enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...options,
      onSuccess: (data) => {
        console.log(data, "Slaughhter unit data");
        options.onSuccess && options.onSuccess(data);
      },
    });
  };
  //   return useQuery(["fetchSlaughterUnit", data], (data), {

  //   } => DeonarService.getSlaughterUnit(data, config));
  // };

  const saveInspectionDetailsData = (data, config = {}) => {
    return useMutation((data) => DeonarService.saveInspectionData(data), config);
  };

  return {
    fetchDeonarCommon,
    saveDeonarDetails,
    searchDeonarCommon,
    fetchEntryFeeDetailsbyUUID,
    saveStablingDetails,
    isSaving,
    saveError,
    fetchTradingList,
    fetchStablingList,
    fetchDawanwalaList,
    fetchHelkariList,
    fetchGatePassSearchData,
    saveGatePassData,
    saveSlaughterListData,
    fetchSlaughterUnit,
    saveInspectionDetailsData,
  };
};

export default useDeonarCommon;
