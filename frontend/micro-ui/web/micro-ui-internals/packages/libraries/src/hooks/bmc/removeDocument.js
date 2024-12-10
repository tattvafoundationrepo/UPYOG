import { useMutation } from "react-query";
import SchemeService from "../../services/elements/Scheme";

export const useRemoveDocuments = (config = {}) => {
  return useMutation((data) => SchemeService.removeDocuments(data), config);
};

export default useRemoveDocuments;
