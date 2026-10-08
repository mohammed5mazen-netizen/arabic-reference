const numbers = new Intl.NumberFormat("ar-EG-u-nu-latn");

export function formatNumber(value: number): string {
  return numbers.format(value);
}
