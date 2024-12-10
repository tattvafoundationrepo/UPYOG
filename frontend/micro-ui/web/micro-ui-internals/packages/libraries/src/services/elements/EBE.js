import Urls from "../atoms/urls";
import { Request } from "../atoms/Utils/Request";

const EBEService = {
    employeeSearch: (data,executeFlag=false) => {
        if (!executeFlag) {
            return;
        }
        return Request({
            url: Urls.EBE.EmployeeSearch,
            data:data,
            method: "POST",
            auth: true,
            userService: true,
        });
    },
    createEnquiry:(data) => {
        return Request({
            url: Urls.EBE.CreateEnquiry,
            data: data,
            useCache: false,
            method: "POST",
            auth: true,
            userService: true,
        });
    },
    getEstimatedID:(data,executeFlag=false)=>{
        if (!executeFlag) {
            return;
        }
        return Request({
            url:Urls.EBE.getEstimatedID,
            data:data,
            useCache: false,
            method: "POST",
            auth: true,
            userService: true,
        });
    },
    getEnquiry:(data,executeFlag=false)=>{
        if (!executeFlag) {
            return;
        }
        return Request({
            url:Urls.EBE.getEnquiry,
            data:data,
            useCache: true,
            method: "POST",
            auth: true,
            userService: true,
        });
    },
    ProcessEnquiry:(data) => {
        return Request({
            url: Urls.EBE.ProcessEnquiry,
            data: data,
            useCache: false,
            method: "POST",
            auth: true,
            userService: true,
        });
    },
    getEBECount: (data) => {
    return Request({
      data: data,
      url: Urls.EBE.getEBECount,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
    });
  },
};

export default EBEService;