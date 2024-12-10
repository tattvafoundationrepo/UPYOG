import { useMutation } from "react-query";
import SchemeService from "../../services/elements/Scheme";

export const useSaveDocument = (config = {}) => {
  return useMutation((data) => SchemeService.saveDocument(data), config);
};

export default useSaveDocument;
