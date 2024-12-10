
export const pdfDownloadLink = (documents = {}, fileStoreId = "", format = "") => {
  /* Need to enhance this util to return required format*/

  let downloadLink = documents[fileStoreId] || "";
  let differentFormats = downloadLink?.split(",") || [];
  let fileURL = "";
  differentFormats.length > 0 &&
    differentFormats.map((link) => {
      if (!link.includes("large") && !link.includes("medium") && !link.includes("small")) {
        fileURL = link;
      }
    });
  return fileURL;
};

/*   method to get filename  from fielstore url*/
export const pdfDocumentName = (documentLink = "", index = 0) => {
  let documentName = decodeURIComponent(documentLink.split("?")[0].split("/").pop().slice(13)) || `Document - ${index + 1}`;
  return documentName;
};

/* methid to get date from epoch */
export const convertEpochToDate = (dateEpoch) => {
  // Returning null in else case because new Date(null) returns initial date from calender
  if (dateEpoch) {
    const dateFromApi = new Date(dateEpoch);
    let month = dateFromApi.getMonth() + 1;
    let day = dateFromApi.getDate();
    let year = dateFromApi.getFullYear();
    month = (month > 9 ? "" : "0") + month;
    day = (day > 9 ? "" : "0") + day;
    return `${year}-${month}-${day}`;
  } else {
    return null;
  }
};

export const convertEpochFormateToDate = (dateEpoch) => {
  // Returning null in else case because new Date(null) returns initial date from calender
  if (dateEpoch) {
    const dateFromApi = new Date(dateEpoch);
    let month = dateFromApi.getMonth() + 1;
    let day = dateFromApi.getDate();
    let year = dateFromApi.getFullYear();
    month = (month > 9 ? "" : "0") + month;
    day = (day > 9 ? "" : "0") + day;
    return `${day}/${month}/${year}`;
  } else {
    return null;
  }
};

const objectsEqual = (o1, o2) => Object.keys(o1).length === Object.keys(o2).length && Object.keys(o1).every((p) => o1[p] === o2[p]);

export const arraysEqual = (a1, a2) => a1.length === a2.length && a1.every((o, idx) => objectsEqual(o, a2[idx]));


/* function returns only the city which user has access to  */
/* exceptional incase of state level user , where return all cities*/
export const getCityThatUserhasAccess = (cities = []) => {
  const userInfo = Digit.UserService.getUser();
  let roleObject = {};
  userInfo?.info?.roles.map((roleData) => { roleObject[roleData?.code] = roleObject[roleData?.code] ? [...roleObject[roleData?.code], roleData?.tenantId] : [roleData?.tenantId] });
  const tenant = Digit.SessionStorage.get("CITIZEN.COMMON.HOME.CITY")?.code || Digit.ULBService.getCurrentTenantId();
  if (roleObject[Digit.Utils?.hrmsRoles?.[0]].includes(Digit.ULBService.getStateId())) {
    return cities;
  }
  return cities.filter(city => roleObject[Digit.Utils?.hrmsRoles?.[0]]?.includes(city?.code));

}

export const convertMillisecondsToYears = (milliseconds) => {
  console.log("milliseconds", milliseconds)
  // Returning null if the input is not provided or invalid
  if (milliseconds) {
    const millisecondsPerDay = 24 * 60 * 60 * 1000 * 365;
    const days = milliseconds / millisecondsPerDay;
    return Math.floor(days); // You can also choose to return a floating-point number if you need precision
  } else {
    return null;
  }
};

export const convertCriteria = (t,criteriatype, criteriacondtion, criteriavalue) => {
  let returnString = t(criteriatype) + " " + t(criteriacondtion) + " ";
  switch (criteriatype) {
    case 'Income': 
      returnString += "₹" + criteriavalue;
      break;
    case 'Age': 
      returnString += criteriavalue + t("YEARS");
      break;
    case 'Disability': 
      returnString += criteriavalue + ' %';
      break;
    case 'Education': 
          switch(criteriavalue){
            case '1': returnString += t("Primary"); break;
            case '2': returnString += t("High School"); break;
            case '3': returnString += t("Senior Secondary"); break;
            case '4': returnString += t("Diploma"); break;
            case '5': returnString += t("Graduation"); break;
            case '6': returnString += t("Postgraduate Diploma"); break;
            default: returnString += t("Postgraduation"); break;
          }
      // No action needed here if Education doesn't add anything to returnString
          break;
    case 'Benefitted': 
      returnString += convertMillisecondsToYears(criteriavalue ) + t("YEARS");  // Adjust unit if you want 'days' or 'years'
      break;
    default: 
      returnString += t(criteriavalue);
      break;
  }
  return returnString;
};