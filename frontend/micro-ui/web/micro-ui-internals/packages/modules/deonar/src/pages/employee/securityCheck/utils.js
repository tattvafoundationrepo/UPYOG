export const validateImportPermissionNumber = (value) => {
  return !value || value.trim() === "" ? "Import Permission Number is required" : "";
};

export const validateVehicleNumber = (value) => {
  return !value || value.trim() === "" ? "Vehicle Number is required" : "";
};
