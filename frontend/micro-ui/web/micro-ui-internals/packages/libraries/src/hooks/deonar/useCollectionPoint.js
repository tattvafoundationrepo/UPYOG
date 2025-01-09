// src/hooks/useCollectionPoint.js
import { useQuery, useMutation, useQueryClient } from "react-query";
import { useDispatch } from "react-redux";
import DeonarService from "../../services/elements/deonarService";

const useCollectionPoint = ({ value }) => {
  const queryClient = useQueryClient();
  const dispatch = useDispatch();
  // Fetching data
  const fetchEntryCollectionFee = (data, config = {}) => {
    return useQuery(["fetchEntryFeeDetails", data], () => DeonarService.getCollectionEntryFee(data), {
      ...config,
      onSuccess: (data) => {
        console.log(data, "entry collection data");
      },
      enabled: value === "arrival",
      staleTime: 0,
      cacheTime: 0,
      refetchOnWindowFocus: true,
    });
  };

  const fetchStablingCollectionFee = (data, config = {}) => {
    return useQuery(["StablingFee", data], () => DeonarService.getCollectionStablingFee(data), {
      ...config,
      onSuccess: (data) => {
        console.log(data, "Stabling collection data");
      },
      enabled: value === "stabling",
      staleTime: 0,
      cacheTime: 0,
      refetchOnWindowFocus: true,
    });
  };

  const { mutateAsync: saveCollectionEntryFee } = useMutation((data) => DeonarService.saveCollectionEntryFee(data), {
    onSuccess: async () => {
      queryClient.invalidateQueries({
        queryKey: ["fetchEntryFeeDetails"],
        exact: false,
      });
    },
    onError: (error) => {
      console.error("Error saving Deonar details:", error);
      throw error;
    },
  });

  const saveParkingDetails = useMutation((data) => DeonarService.saveParkingDetail(data), {
    onSuccess: () => {
      queryClient.invalidateQueries("ParkingDetails");
    },
  });

  const saveWashingDetails = useMutation((data) => DeonarService.saveWashingDetail(data), {
    onSuccess: () => {
      queryClient.invalidateQueries("WashingDetails");
    },
  });

  const fetchSlaughterCollectionFee = (data, config = {}) => {
    const { enabled = true, executeOnLoad = false, executeOnRadioSelect = false } = config;
    return useQuery(["SlaughterFee", data], () => DeonarService.getCollectionSlaughterFee(data), {
      enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...config,
      onSuccess: (data) => {
        console.log(data, "SlaughterFee collection data");
      },
      // enabled: value === "slaughter",
    });
  };

  // const fetchSlaughterCollectionFee = (data, config = {}) => {
  //   return useMutation((data) => DeonarService.getCollectionSlaughterFee(data), config);
  // };

  const fetchWashingCollectionFee = (data, config = {}) => {
    const { enabled = true, executeOnLoad = false, executeOnRadioSelect = false } = config;
    return useQuery(["washingFee", data], () => DeonarService.getCollectionWashingFee(data), {
      enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...config,
      onSuccess: (data) => {
        console.log(data, "washing collection data");
      },
    });
  };

  const fetchParkingCollectionFee = (data, config = {}) => {
    const { enabled = true, executeOnLoad = false, executeOnRadioSelect = false } = config;
    return useQuery(["ParkingFee", data], () => DeonarService.getCollectionParkingFee(data), {
      enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...config,
      onSuccess: (data) => {
        console.log(data, "Parking collection data");
      },
    });
  };

  const fetchParkingCollectionDetails = (data, config = {}, enabled = true) => {
    return useQuery(["ParkingDetails", data], () => DeonarService.getCollectionParkingDetails(data), {
      ...config,
      // Disable caching to always fetch fresh data
      cacheTime: 0,
      // Stale immediately after fetching
      staleTime: 0,
      enabled,
    });
  };

  const fetchWashingCollectionDetails = (data, config = {}, enabled = true) => {
    return useQuery(["WashingDetails", data], () => DeonarService.getWashingDetails(data), {
      ...config,
      // Disable caching to always fetch fresh data
      cacheTime: 0,
      // Stale immediately after fetching
      staleTime: 0,
      enabled,
    });
  };

  const fetchSlaughterCollectionList = (data, config = {}) => {
    const { enabled = true, executeOnLoad = false, executeOnRadioSelect = false } = config;
    return useQuery(["SlaughterList", data], () => DeonarService.getCollectionSlaughterList(data), {
      enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...config,
      onSuccess: (data) => {
        console.log(data, "SlaughterList collection data");
      },
    });
  };

  const fetchRemovalList = (data, config = {}) => {
    const { enabled = true, executeOnLoad = false, executeOnRadioSelect = false } = config;
    return useQuery(["RemovalList", data], () => DeonarService.getCollectionRemovalList(data), {
      enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...config,
      onSuccess: (data) => {
        console.log(data, "RemovalList collection data");
      },
    });
  };

  const fetchRemovalCollectionFee = (data, config = {}) => {
    const { enabled = true, executeOnLoad = false, executeOnRadioSelect = false } = config;
    return useQuery(["removalFee", data], () => DeonarService.getCollectionRemovalFee(data), {
      enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...config,
      onSuccess: (data) => {
        console.log(data, "removalFee collection data");
      },
      // enabled: value === "removal",
    });
  };

  const fetchTradingCollectionFee = (data, config = {}) => {
    return useQuery(["tradingFee", data], () => DeonarService.getCollectionTradingFee(data), {
      ...config,
      onSuccess: (data) => {
        console.log(data, "tradingFee collection data");
      },
      enabled: value === "trading",
    });
  };

  const fetchPenaltiesList = (data, config = {}) => {
    return useQuery(["PenaltiesList", data], () => DeonarService.getCollectionPenaltiesList(data), {
      ...config,
      onSuccess: (data) => {
        console.log(data, "PenaltiesList collection data");
      },
      enabled: value === "penalty",
    });
  };

  const fetchweighingList = (data, config = {}) => {
    // const { enabled = true, executeOnLoad = false, executeOnRadioSelect = false } = config;
    return useQuery(["weighingList", data], () => DeonarService.getCollectionWeighingList(data), {
      // enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...config,
      onSuccess: (data) => {
        console.log(data, "weighingList collection data");
      },
      enabled: value === "weighing",
    });
  };

  const fetchweighingFee = (data, config = {}) => {
    // const { enabled = true, executeOnLoad = false, executeOnRadioSelect = false } = config;
    return useQuery(["weighingFee", data], () => DeonarService.fetchWeighingCollectionFee(data), {
      // enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...config,
      onSuccess: (data) => {
        console.log(data, "weighing collection data");
      },
    });
  };
  const fetchEmergencySlaughterList = (data, config = {}) => {
    return useQuery(["EmergencySlaughterList", data], () => DeonarService.getEmergencySlaughter(data), {
      ...config,
      onSuccess: (data) => {
        console.log(data, "EmergencySlaughterList collection data");
      },
    });
  };
  const fetchExportSlaughterList = (data, config = {}) => {
    return useQuery(["ExportSlaughterList", data], () => DeonarService.getExportSlaughterList(data), {
      ...config,
      onSuccess: (data) => {
        console.log(data, "ExportSlaughterList collection data");
      },
    });
  };
  const fetchRemovalReport = (data, config = {}) => {
    return useQuery(["RemovalReport", data], () => DeonarService.getRemovalReports(data), {
      ...config,
      onSuccess: (data) => {
        console.log(data, "RemovalReport collection data");
      },
    });
  };

  const fetchNormalSlaughterList = (data, config = {}) => {
    const { enabled = true, executeOnLoad = false, executeOnRadioSelect = false } = config;
    return useQuery(["NormalSlaughterList", data], () => DeonarService.getNormalSlaughterList(data), {
      enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...config,
      onSuccess: (data) => {
        console.log(data, "NormalSlaughterList collection data");
      },
    });
  };

  const fectchCollectionStablingList = (data, config = {}) => {
    const { enabled = true, executeOnLoad = false, executeOnRadioSelect = false } = config;
    return useQuery(["StablingList", data], () => DeonarService.getCollectionStablingList(data), {
      enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...config,
      onSuccess: (data) => {
        console.log(data, "StablingList collection data");
      },
    });
  };

  const fetchCollectionEntryList = (data, config = {}) => {
    // const { enabled = true, executeOnLoad = false, executeOnRadioSelect = false } = config;
    return useQuery(["EntryList", data], () => DeonarService.getCollectionEntryList(data), {
      // enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...config,
      onSuccess: (data) => {
        console.log(data, "EntryList collection data");
      },
    });
  };

  const fetchRemovalCollectionList = (data, config = {}) => {
    const { enabled = true, executeOnLoad = false, executeOnRadioSelect = false } = config;
    return useQuery(["RemovalList", data], () => DeonarService.getRemovalCollectionList(data), {
      enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...config,
      onSuccess: (data) => {
        console.log(data, "RemovalList collection data");
      },
    });
  };

  const fetchCollectionSlaughterList = (data, config = {}) => {
    const { enabled = true, executeOnLoad = false, executeOnRadioSelect = false } = config;
    return useQuery(["SlaughterList", data], () => DeonarService.getSlaughterCollectionList(data), {
      enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...config,
      onSuccess: (data) => {
        console.log(data, "SlaughterList collection data");
      },
    });
  };
  const fetchCitizensList = (data, config = {}) => {
    // const { enabled = true, executeOnLoad = false, executeOnRadioSelect = false } = config;
    return useQuery(["CistizenList", data], () => DeonarService.getCitizenList(data), {
      // enabled: enabled && (executeOnLoad || executeOnRadioSelect),
      ...config,
      onSuccess: (data) => {
        console.log(data, "Citizen collection data");
      },
    });
  };

  return {
    fetchEntryCollectionFee,
    fetchStablingCollectionFee,
    fetchWashingCollectionFee,
    fetchParkingCollectionFee,
    saveCollectionEntryFee,
    fetchSlaughterCollectionFee,
    fetchParkingCollectionDetails,
    fetchSlaughterCollectionList,
    fetchRemovalCollectionFee,
    fetchRemovalList,
    fetchTradingCollectionFee,
    fetchPenaltiesList,
    fetchweighingList,
    saveParkingDetails,
    fetchweighingFee,
    fetchEmergencySlaughterList,
    fetchExportSlaughterList,
    fetchRemovalReport,
    saveWashingDetails,
    fetchWashingCollectionDetails,
    fetchNormalSlaughterList,
    fectchCollectionStablingList,
    fetchCollectionEntryList,
    fetchRemovalCollectionList,
    fetchCollectionSlaughterList,
    fetchCitizensList,
  };
};

export default useCollectionPoint;
