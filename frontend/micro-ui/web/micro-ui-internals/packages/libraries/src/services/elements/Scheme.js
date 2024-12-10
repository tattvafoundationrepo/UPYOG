import useSaveBankDetails from "../../hooks/bmc/saveBankDetails";
import Urls from "../atoms/urls";
import { Request } from "../atoms/Utils/Request";

const SchemeService = {
  get: (data) => {
    return Request({
      data: data,
      url: Urls.common.get,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  getSchemes: (data) => {
    return Request({
      data: data,
      url: Urls.schemes.getSchemes,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  getUserDetails: (data) => {
    return Request({
      data: data,
      url: Urls.users.getUserDetails,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
      headers: {
        "Cache-Control": "no-cache",
        Pragma: "no-cache",
        Expires: "0",
      },
    });
  },
  saveUserDetails: (data) => {
    return Request({
      data: data,
      url: Urls.users.saveUserDetails,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  saveSchemeDetails: (data) => {
    return Request({
      data: data,
      url: Urls.schemes.saveScheme,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  saveDocument: (data) => {
    return Request({
      data: data,
      url: Urls.schemes.saveDocuments,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });

  },
  saveQualification: (data) => {
    return Request({
      data: data,
      url: Urls.schemes.saveQualifications,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });

  },
  saveBankDetails: (data) => {
    return Request({
      data: data,
      url: Urls.schemes.saveBanks,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });

  },

  removeDocuments: (data) => {
    return Request({
      data: data,
      url: Urls.schemes.removeDocument,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    })
  },

  getVerifierSchemes: (data) => {
    return Request({
      data: data,
      url: Urls.schemes.getVerifierScheme,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getverifyScheme: (data) => {
    return Request({
      data: data,
      url: Urls.schemes.verifyScheme,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getBanks: (data) => {
    return Request({
      data: data,
      url: Urls.common.getBanks,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  getCount: (data) => {
    return Request({
      data: data,
      url: Urls.schemes.getCount,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },

  getAllApplications: (data) => {
    return Request({
      data: data,
      url: Urls.users.getAllApplications,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
  getAllinboxApplications: (data) => {
    return Request({
      data: data,
      url: Urls.users.getAllBMCInboxApplication,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    })
  }
};

export default SchemeService;
