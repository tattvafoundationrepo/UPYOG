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
      refetchOnWindowFocus: true,    });
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
  // const fetchSlaughterCollectionFee = (data, config = {}) => {
  //   return useQuery(["SlaughterFee", data], () => DeonarService.getCollectionSlaughterFee(data), {
  //     ...config,
  //     onSuccess: (data) => {
  //       console.log(data, "SlaughterFee collection data");
  //     },
  //     // enabled: value === "slaughter",
  //   });
  // };

  const fetchSlaughterCollectionFee = (data, config = {}) => {
    return useMutation((data) => DeonarService.getCollectionSlaughterFee(data), config);
  };

  const fetchWashingCollectionFee = (data, config = {}) => {
    return useQuery(["washingFee", data], () => DeonarService.getCollectionWashingFee(data), {
      ...config,
      onSuccess: (data) => {
        console.log(data, "washingFee collection data");
      },
      // enabled: value === "washing",
    });
  };

  const fetchParkingCollectionFee = (data, config = {}) => {
    return useQuery(["ParkingFee", data], () => DeonarService.getCollectionParkingFee(data), {
      ...config,
      onSuccess: (data) => {
        console.log(data, "Parking collection data");
      },
      // enabled: value === "parking",
    });
  };
  const fetchParkingCollectionDetails = (data, config = {}, enabled = true) => {
    return useQuery(["ParkingDetails", data], 
      () => DeonarService.getCollectionParkingDetails(data), 
      {
        ...config,
        // Disable caching to always fetch fresh data
        cacheTime: 0,
        // Stale immediately after fetching
        staleTime: 0,
        enabled,
      }
    );
  };

  const fetchSlaughterCollectionList = (data, config = {}) => {
    return useQuery(["SlaughterList", data], () => DeonarService.getCollectionSlaughterList(data), {
      ...config,
      onSuccess: (data) => {
        console.log(data, "SlaughterList collection data");
      },
      // enabled: value === "slaughter",
    });
  };

  const fetchRemovalList = (data, config = {}) => {
    return useQuery(["RemovalList", data], () => DeonarService.getCollectionRemovalList(data), {
      ...config,
      onSuccess: (data) => {
        console.log(data, "RemovalList collection data");
      },
      // enabled: value === "removal",
    });
  };

  const fetchRemovalCollectionFee = (data, config = {}) => {
    return useQuery(["removalFee", data], () => DeonarService.getCollectionRemovalFee(data), {
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
    return useQuery(["weighingList", data], () => DeonarService.getCollectionWeighingList(data), {
      ...config,
      onSuccess: (data) => {
        console.log(data, "weighingList collection data");
      },
      // enabled: value === "weighing",
    });
  };

  const fetchweighingFee = (data, config = {}) => {
    return useQuery(["weighingFee", data], () => DeonarService.fetchWeighingCollectionFee(data), {
      ...config,
      onSuccess: (data) => {
        console.log(data, "weighing collection data");
      },
      // enabled: value === "weighing",
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
  };
};

export default useCollectionPoint;
