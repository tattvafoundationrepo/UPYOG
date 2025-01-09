import Urls from "../atoms/urls";
import { Request } from "../atoms/Utils/Request";

const DeonarService = {
  DeonarService: (data) => {
    return Request({
      data: data,
      url: Urls.common.getDeonarCommon,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  getStakeholders: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getStakeholders,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getStakeholder: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getStakeholder,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  saveDeonarUserDetails: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.saveDeonarDetails,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  searchDeonarDetails: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.searchDeonarDetails,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  saveInspectionPoint: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.saveInspectionPoint,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getInspectionPoint: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getInspectionPoint,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  saveStablingPoint: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.saveStablingPoint,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  savePrakingDetail: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.savePrakingDetails,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  saveParkingDetail: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.savePrakingDetails,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  saveWashingDetail: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.saveWashingDetails,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getPrakingDetail: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getPrakingDetails,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getWashingDetails: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getWashingDetails,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getCollectionEntryFee: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getCollectionEntryFee,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getCollectionStablingFee: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getCollectionStablingFee,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  saveCollectionEntryFee: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.saveCollectionEntryFee,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getCollectionWashingFee: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getCollectionWashingFee,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getCollectionParkingFee: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getCollectionParkingFee,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getCollectionSlaughterFee: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getCollectionSlaughterFee,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getCollectionParkingDetails: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getCollectionParkingDetails,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getCollectionSlaughterList: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getCollectionSlaughterList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getTradingList: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getTradingList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getStablingList: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getStablingList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  getDawanwalaList: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getDawanwalaList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  getHelkariList: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getHelkariList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  getCollectionRemovalList: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getCollectionRemovalList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getCollectionRemovalFee: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getCollectionRemovalFee,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getCollectionTradingFee: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getCollectionTradingFee,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getGatePassSearchData: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getGatePassSearchData,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  saveGatePassData: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.saveGatePassData,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  getPenalties: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getPenalties,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  savePenalties: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.savePenalties,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getCollectionPenaltiesList: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getCollectionPenaltiesList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getCollectionWeighingList: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getCollectionWeighingList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  fetchWeighingCollectionFee: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.fetchWeighingCollectionFee,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  saveSlaughterListData: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.saveSlaughterListData,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getSlaughterUnit: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getSlaughterUnit,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  saveInspectionData: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.saveInspectionData,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  saveSlaughterBooking: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.saveSlaughterListDataBooking,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  saveStakeholderDetails: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.saveStakeholderDetails,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  getEmergencySlaughter: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getEmergencySlaughterList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  getExportSlaughterList: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getEmergencyExportList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  getRemovalReports: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getRemovalReport,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getNormalSlaughterList: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getNormalSlaughterList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getCollectionStablingList: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getCollectionStablingList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getCollectionEntryList: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getCollectionEntryList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getRemovalCollectionList: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getRemovalCollectionList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getSlaughterCollectionList: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getSlaughterCollectionList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getCitizenList: (data) => {
    return Request({
      data: data,
      url: Urls.deonar_security_check.getCitizensList,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,

    })
  }
};

export default DeonarService;
